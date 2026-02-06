import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { PeopleService, PersonSummary } from '../core/services/people.service';

@Component({
  selector: 'app-people',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="space-y-8">
      <h1 class="text-2xl font-bold text-gray-900">People / Ledger</h1>

      <!-- Create person -->
      <section>
        <h2 class="text-lg font-semibold text-gray-800 mb-3">Add Person</h2>
        <form [formGroup]="form" (ngSubmit)="createPerson()" class="flex flex-wrap gap-4 items-end">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Name *</label>
            <input formControlName="name" class="rounded-md border border-gray-300 px-3 py-2 w-48" placeholder="Name" />
            @if (form.get('name')?.invalid && form.get('name')?.touched) {
              <p class="text-sm text-red-600">Required</p>
            }
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Phone</label>
            <input formControlName="phone" class="rounded-md border border-gray-300 px-3 py-2 w-40" placeholder="Phone" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Notes</label>
            <input formControlName="notes" class="rounded-md border border-gray-300 px-3 py-2 w-64" placeholder="Notes" />
          </div>
          <button type="submit" [disabled]="form.invalid || createLoading"
            class="bg-primary-600 text-white py-2 px-4 rounded-md hover:bg-primary-700 disabled:opacity-50">
            @if (createLoading) { Adding… } @else { Add Person }
          </button>
          @if (createError) {
            <span class="text-red-600 text-sm">{{ createError }}</span>
          }
          @if (deleteError) {
            <span class="text-red-600 text-sm ml-2">{{ deleteError }}</span>
          }
        </form>
      </section>

      <!-- List people / ledger -->
      <section>
        <h2 class="text-lg font-semibold text-gray-800 mb-3">Ledger (per person)</h2>
        @if (loading) {
          <p class="text-gray-500">Loading…</p>
        } @else if (people.length === 0) {
          <p class="text-gray-500">No people yet. Add someone above.</p>
        } @else {
          <div class="overflow-x-auto">
            <table class="min-w-full bg-white border border-gray-200 rounded-lg shadow">
              <thead class="bg-gray-50">
                <tr>
                  <th class="px-4 py-2 text-left text-sm font-medium text-gray-700">Name</th>
                  <th class="px-4 py-2 text-right text-sm font-medium text-gray-700">Received</th>
                  <th class="px-4 py-2 text-right text-sm font-medium text-gray-700">Given</th>
                  <th class="px-4 py-2 text-right text-sm font-medium text-gray-700">Net</th>
                  <th class="px-4 py-2 text-left text-sm font-medium text-gray-700">Status</th>
                  <th class="px-4 py-2 text-left text-sm font-medium text-gray-700">Actions</th>
                </tr>
              </thead>
              <tbody>
                @for (p of people; track p.id) {
                  <tr class="border-t border-gray-200 hover:bg-gray-50">
                    <td class="px-4 py-3 font-medium text-gray-900">{{ p.name }}</td>
                    <td class="px-4 py-3 text-right text-green-600">{{ p.totalReceived | number:'1.2-2' }}</td>
                    <td class="px-4 py-3 text-right text-red-600">{{ p.totalGiven | number:'1.2-2' }}</td>
                    <td class="px-4 py-3 text-right font-medium">{{ p.netBalance | number:'1.2-2' }}</td>
                    <td class="px-4 py-3">
                      @switch (p.status) {
                        @case ('THEY_OWE_ME') { <span class="text-green-600">They owe me</span> }
                        @case ('I_OWE_THEM') { <span class="text-red-600">I owe them</span> }
                        @default { <span class="text-gray-500">Settled</span> }
                      }
                    </td>
                    <td class="px-4 py-3">
                      <button type="button" (click)="deletePerson(p)" [disabled]="p.totalReceived > 0 || p.totalGiven > 0 || deleteLoading"
                        class="text-red-600 hover:text-red-800 text-sm disabled:opacity-50 disabled:cursor-not-allowed">
                        {{ deleteLoading ? '…' : 'Delete' }}
                      </button>
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        }
      </section>
    </div>
  `,
})
export class PeopleComponent implements OnInit {
  people: PersonSummary[] = [];
  loading = true;
  form: FormGroup;
  createLoading = false;
  createError = '';
  deleteLoading = false;
  deleteError = '';

  constructor(
    private fb: FormBuilder,
    private peopleService: PeopleService
  ) {
    this.form = this.fb.group({
      name: ['', Validators.required],
      phone: [''],
      notes: [''],
    });
  }

  ngOnInit(): void {
    this.loadPeople();
  }

  deletePerson(p: PersonSummary): void {
    if (p.totalReceived > 0 || p.totalGiven > 0) return;
    if (!confirm('Delete person "' + p.name + '"?')) return;
    this.deleteLoading = true;
    this.deleteError = '';
    this.peopleService.delete(p.id).subscribe({
      next: () => {
        this.deleteLoading = false;
        this.loadPeople();
      },
      error: (err) => {
        this.deleteLoading = false;
        this.deleteError = err.error?.message || err.error || 'Cannot delete.';
      },
    });
  }

  loadPeople(): void {
    this.loading = true;
    this.peopleService.list().subscribe({
      next: (list) => {
        this.people = list;
        this.loading = false;
      },
      error: () => { this.loading = false; },
    });
  }

  createPerson(): void {
    if (this.form.invalid || this.createLoading) return;
    this.createLoading = true;
    this.createError = '';
    this.peopleService.create(
      this.form.value.name,
      this.form.value.phone || undefined,
      this.form.value.notes || undefined
    ).subscribe({
      next: () => {
        this.createLoading = false;
        this.form.reset({ name: '', phone: '', notes: '' });
        this.loadPeople();
      },
      error: (err) => {
        this.createLoading = false;
        this.createError = err.error?.message || err.error || 'Failed to add person.';
      },
    });
  }
}
