import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { WalletService, WalletResponse, WalletType } from '../core/services/wallet.service';
import { TransactionService } from '../core/services/transaction.service';

@Component({
  selector: 'app-wallets',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="space-y-8">
      <h1 class="text-2xl font-bold text-gray-900">Wallets</h1>

      <!-- List wallets -->
      <section>
        <h2 class="text-lg font-semibold text-gray-800 mb-3">My Wallets</h2>
        @if (walletsLoading) {
          <p class="text-gray-500">Loading…</p>
        } @else if (wallets.length === 0) {
          <p class="text-gray-500">No wallets yet. Create CASH and/or ONLINE below.</p>
        } @else {
          <ul class="space-y-2">
            @for (w of wallets; track w.id) {
              <li class="flex items-center justify-between bg-white rounded-lg shadow p-4 border border-gray-200">
                <span class="font-medium">{{ w.type }}</span>
                <span class="text-lg font-bold">{{ w.balance | number:'1.2-2' }}</span>
              </li>
            }
          </ul>
        }
      </section>

      <!-- Create wallet -->
      <section>
        <h2 class="text-lg font-semibold text-gray-800 mb-3">Create Wallet</h2>
        <form [formGroup]="createForm" (ngSubmit)="createWallet()" class="flex gap-4 items-end flex-wrap">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Type</label>
            <select formControlName="type" class="rounded-md border border-gray-300 px-3 py-2">
              <option value="CASH">CASH</option>
              <option value="ONLINE">ONLINE</option>
            </select>
          </div>
          <button type="submit" [disabled]="createForm.invalid || createLoading"
            class="bg-primary-600 text-white py-2 px-4 rounded-md hover:bg-primary-700 disabled:opacity-50">
            @if (createLoading) { Creating… } @else { Create }
          </button>
          @if (createError) {
            <span class="text-red-600 text-sm">{{ createError }}</span>
          }
        </form>
      </section>

      <!-- Transfer -->
      <section>
        <h2 class="text-lg font-semibold text-gray-800 mb-3">Transfer (Cash ↔ Online)</h2>
        <form [formGroup]="transferForm" (ngSubmit)="transfer()" class="space-y-4 max-w-md">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">From</label>
            <select formControlName="fromWalletId" class="w-full rounded-md border border-gray-300 px-3 py-2">
              <option [ngValue]="null">Select wallet</option>
              @for (w of wallets; track w.id) {
                <option [ngValue]="w.id">{{ w.type }} ({{ w.balance | number:'1.2-2' }})</option>
              }
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">To</label>
            <select formControlName="toWalletId" class="w-full rounded-md border border-gray-300 px-3 py-2">
              <option [ngValue]="null">Select wallet</option>
              @for (w of wallets; track w.id) {
                <option [ngValue]="w.id">{{ w.type }} ({{ w.balance | number:'1.2-2' }})</option>
              }
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Amount</label>
            <input type="number" formControlName="amount" step="0.01" min="0.01"
              class="w-full rounded-md border border-gray-300 px-3 py-2" />
            @if (transferForm.get('amount')?.invalid && transferForm.get('amount')?.touched) {
              <p class="text-sm text-red-600">Min 0.01</p>
            }
          </div>
          <button type="submit" [disabled]="transferForm.invalid || transferLoading"
            class="bg-primary-600 text-white py-2 px-4 rounded-md hover:bg-primary-700 disabled:opacity-50">
            @if (transferLoading) { Transferring… } @else { Transfer }
          </button>
          @if (transferError) {
            <p class="text-red-600 text-sm">{{ transferError }}</p>
          }
        </form>
      </section>
    </div>
  `,
})
export class WalletsComponent implements OnInit {
  wallets: WalletResponse[] = [];
  walletsLoading = true;
  createForm: FormGroup;
  createLoading = false;
  createError = '';
  transferForm: FormGroup;
  transferLoading = false;
  transferError = '';

  constructor(
    private fb: FormBuilder,
    private walletService: WalletService,
    private transactionService: TransactionService
  ) {
    this.createForm = this.fb.group({ type: ['CASH', Validators.required] });
    this.transferForm = this.fb.group({
      fromWalletId: [null as number | null, Validators.required],
      toWalletId: [null as number | null, Validators.required],
      amount: [null as number | null, [Validators.required, Validators.min(0.01)]],
    });
  }

  ngOnInit(): void {
    this.loadWallets();
  }

  loadWallets(): void {
    this.walletsLoading = true;
    this.walletService.list().subscribe({
      next: (list) => {
        this.wallets = list;
        this.walletsLoading = false;
      },
      error: () => { this.walletsLoading = false; },
    });
  }

  createWallet(): void {
    if (this.createForm.invalid || this.createLoading) return;
    this.createLoading = true;
    this.createError = '';
    this.walletService.create(this.createForm.value.type as WalletType).subscribe({
      next: () => {
        this.createLoading = false;
        this.loadWallets();
      },
      error: (err) => {
        this.createLoading = false;
        this.createError = err.error?.message || err.error || 'Failed to create wallet.';
      },
    });
  }

  transfer(): void {
    if (this.transferForm.invalid || this.transferLoading) return;
    const from = this.transferForm.value.fromWalletId as number;
    const to = this.transferForm.value.toWalletId as number;
    if (from === to) {
      this.transferError = 'From and To must be different.';
      return;
    }
    this.transferLoading = true;
    this.transferError = '';
    this.transactionService.create({
      transactionType: 'TRANSFER',
      fromWalletId: from,
      toWalletId: to,
      amount: this.transferForm.value.amount,
    }).subscribe({
      next: () => {
        this.transferLoading = false;
        this.transferForm.patchValue({ amount: null });
        this.loadWallets();
      },
      error: (err) => {
        this.transferLoading = false;
        this.transferError = err.error?.message || err.error || 'Transfer failed.';
      },
    });
  }
}
