import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { TransactionService, TransactionResponse, TransactionType, CreateTransactionRequest, WalletType } from '../core/services/transaction.service';
import { WalletService, WalletResponse } from '../core/services/wallet.service';
import { PeopleService, PersonSummary } from '../core/services/people.service';

@Component({
  selector: 'app-transactions',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  template: `
    <div class="space-y-8">
      <h1 class="text-2xl font-bold text-gray-900">Transactions</h1>

      <!-- Create transaction -->
      <section class="bg-white rounded-lg shadow p-6 border border-gray-200">
        <h2 class="text-lg font-semibold text-gray-800 mb-4">New Transaction</h2>
        <form [formGroup]="form" (ngSubmit)="create()" class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Type</label>
            <select formControlName="transactionType" class="w-full rounded-md border border-gray-300 px-3 py-2">
              <option value="RECEIVED">RECEIVED</option>
              <option value="GIVEN">GIVEN</option>
              <option value="EXPENSE">EXPENSE</option>
              <option value="INCOME">INCOME</option>
              <option value="TRANSFER">TRANSFER</option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Amount *</label>
            <input type="number" formControlName="amount" step="0.01" min="0.01" class="w-full rounded-md border border-gray-300 px-3 py-2" />
          </div>
          @if (needsPerson()) {
            <div class="md:col-span-2">
              <label class="block text-sm font-medium text-gray-700 mb-1">Person</label>
              <select formControlName="personId" class="w-full rounded-md border border-gray-300 px-3 py-2">
                <option [ngValue]="null">Select person</option>
                @for (p of people; track p.id) {
                  <option [ngValue]="p.id">{{ p.name }}</option>
                }
              </select>
            </div>
          }
          @if (needsFromWallet()) {
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">From Wallet</label>
              <select formControlName="fromWalletId" class="w-full rounded-md border border-gray-300 px-3 py-2">
                <option [ngValue]="null">Select</option>
                @for (w of wallets; track w.id) {
                  <option [ngValue]="w.id">{{ w.type }} ({{ w.balance | number:'1.2-2' }})</option>
                }
              </select>
            </div>
          }
          @if (needsToWallet()) {
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">To Wallet</label>
              <select formControlName="toWalletId" class="w-full rounded-md border border-gray-300 px-3 py-2">
                <option [ngValue]="null">Select</option>
                @for (w of wallets; track w.id) {
                  <option [ngValue]="w.id">{{ w.type }} ({{ w.balance | number:'1.2-2' }})</option>
                }
              </select>
            </div>
          }
          <div class="md:col-span-2">
            <label class="block text-sm font-medium text-gray-700 mb-1">Description (optional)</label>
            <input formControlName="description" class="w-full rounded-md border border-gray-300 px-3 py-2" placeholder="Notes" />
          </div>
          <div class="md:col-span-2 flex gap-4">
            <button type="submit" [disabled]="form.invalid || createLoading"
              class="bg-primary-600 text-white py-2 px-4 rounded-md hover:bg-primary-700 disabled:opacity-50">
              @if (createLoading) { Saving… } @else { Create Transaction }
            </button>
            @if (createError) {
              <span class="text-red-600 text-sm self-center">{{ createError }}</span>
            }
          </div>
        </form>
      </section>

      <!-- Filters + list -->
      <section>
        <h2 class="text-lg font-semibold text-gray-800 mb-3">Transactions</h2>
        <div class="flex flex-wrap gap-4 mb-4">
          <select [(ngModel)]="filterWallet" (ngModelChange)="load()" class="rounded-md border border-gray-300 px-3 py-2">
            <option [ngValue]="null">All wallets</option>
            <option value="CASH">CASH</option>
            <option value="ONLINE">ONLINE</option>
          </select>
          <select [(ngModel)]="filterType" (ngModelChange)="load()" class="rounded-md border border-gray-300 px-3 py-2">
            <option [ngValue]="null">All types</option>
            <option value="RECEIVED">RECEIVED</option>
            <option value="GIVEN">GIVEN</option>
            <option value="EXPENSE">EXPENSE</option>
            <option value="INCOME">INCOME</option>
            <option value="TRANSFER">TRANSFER</option>
          </select>
        </div>
        @if (listError) {
          <p class="text-red-600 text-sm mb-2">{{ listError }}</p>
        }
        @if (listLoading) {
          <p class="text-gray-500">Loading…</p>
        } @else if (transactions.length === 0) {
          <p class="text-gray-500">No transactions.</p>
        } @else {
          <div class="overflow-x-auto">
            <table class="min-w-full bg-white border border-gray-200 rounded-lg shadow">
              <thead class="bg-gray-50">
                <tr>
                  <th class="px-4 py-2 text-left text-sm font-medium text-gray-700">Date</th>
                  <th class="px-4 py-2 text-left text-sm font-medium text-gray-700">Type</th>
                  <th class="px-4 py-2 text-right text-sm font-medium text-gray-700">Amount</th>
                  <th class="px-4 py-2 text-left text-sm font-medium text-gray-700">Description</th>
                  <th class="px-4 py-2 text-left text-sm font-medium text-gray-700">Actions</th>
                </tr>
              </thead>
              <tbody>
                @for (t of transactions; track t.id) {
                  <tr class="border-t border-gray-200 hover:bg-gray-50">
                    <td class="px-4 py-3 text-sm text-gray-600">{{ t.date | date:'short' }}</td>
                    <td class="px-4 py-3 font-medium">{{ t.transactionType }}</td>
                    <td class="px-4 py-3 text-right font-medium">{{ t.amount | number:'1.2-2' }}</td>
                    <td class="px-4 py-3 text-sm text-gray-600">{{ t.description || '—' }}</td>
                    <td class="px-4 py-3">
                      <button type="button" (click)="reverseTransaction(t)" [disabled]="reverseLoading"
                        class="text-amber-600 hover:text-amber-800 text-sm disabled:opacity-50">
                        {{ reverseLoading ? '…' : 'Reverse' }}
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
export class TransactionsComponent implements OnInit {
  wallets: WalletResponse[] = [];
  people: PersonSummary[] = [];
  transactions: TransactionResponse[] = [];
  listLoading = true;
  form: FormGroup;
  createLoading = false;
  createError = '';
  listError = '';
  reverseLoading = false;
  filterWallet: WalletType | null = null;
  filterType: TransactionType | null = null;

  constructor(
    private fb: FormBuilder,
    private walletService: WalletService,
    private peopleService: PeopleService,
    private transactionService: TransactionService
  ) {
    this.form = this.fb.group({
      transactionType: ['EXPENSE', Validators.required],
      amount: [null as number | null, [Validators.required, Validators.min(0.01)]],
      personId: [null as number | null],
      fromWalletId: [null as number | null],
      toWalletId: [null as number | null],
      description: [''],
    });
  }

  ngOnInit(): void {
    this.walletService.list().subscribe((w) => (this.wallets = w));
    this.peopleService.list().subscribe((p) => (this.people = p));
    this.load();
  }

  needsPerson(): boolean {
    const t = this.form.get('transactionType')?.value as TransactionType;
    return t === 'RECEIVED' || t === 'GIVEN';
  }

  needsFromWallet(): boolean {
    const t = this.form.get('transactionType')?.value as TransactionType;
    return t === 'GIVEN' || t === 'EXPENSE' || t === 'TRANSFER';
  }

  needsToWallet(): boolean {
    const t = this.form.get('transactionType')?.value as TransactionType;
    return t === 'RECEIVED' || t === 'INCOME' || t === 'TRANSFER';
  }

  reverseTransaction(t: TransactionResponse): void {
    if (!confirm('Reverse this transaction? This will create an opposite entry.')) return;
    this.reverseLoading = true;
    this.transactionService.reverse(t.id).subscribe({
      next: () => {
        this.reverseLoading = false;
        this.load();
      },
      error: (err) => {
        this.reverseLoading = false;
        this.createError = err.error?.message || err.error || 'Cannot reverse.';
      },
    });
  }

  load(): void {
    this.listLoading = true;
    this.transactionService.list({
      wallet: this.filterWallet ?? undefined,
      type: this.filterType ?? undefined,
    }).subscribe({
      next: (list) => {
        this.transactions = list;
        this.listLoading = false;
        this.listError = '';
      },
      error: (err) => {
        this.listLoading = false;
        this.listError = err.error?.message || err.error || 'Failed to load transactions.';
      },
    });
  }

  buildRequest(): CreateTransactionRequest | null {
    const type = this.form.value.transactionType as TransactionType;
    const amount = this.form.value.amount as number;
    if (!amount || amount < 0.01) return null;
    const req: CreateTransactionRequest = {
      transactionType: type,
      amount,
      description: this.form.value.description || null,
    };
    if (this.needsPerson()) req.personId = this.form.value.personId;
    if (this.needsFromWallet()) req.fromWalletId = this.form.value.fromWalletId;
    if (this.needsToWallet()) req.toWalletId = this.form.value.toWalletId;
    return req;
  }

  create(): void {
    const req = this.buildRequest();
    if (!req || this.createLoading) return;
    this.createLoading = true;
    this.createError = '';
    this.transactionService.create(req).subscribe({
      next: () => {
        this.createLoading = false;
        this.form.patchValue({ amount: null, description: '' });
        this.form.markAsUntouched();
        this.load();
      },
      error: (err) => {
        this.createLoading = false;
        this.createError = err.error?.message || err.error || 'Failed to create transaction.';
      },
    });
  }
}
