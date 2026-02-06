import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { WalletService, BalanceResponse } from '../core/services/wallet.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="space-y-6">
      <h1 class="text-2xl font-bold text-gray-900">Dashboard</h1>
      @if (loading) {
        <div class="text-gray-500">Loading balances…</div>
      } @else if (error) {
        <div class="p-4 rounded-md bg-red-50 text-red-700">{{ error }}</div>
      } @else if (balance) {
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div class="bg-white rounded-lg shadow p-6 border border-gray-200">
            <h2 class="text-sm font-medium text-gray-500">Total Balance</h2>
            <p class="text-2xl font-bold text-gray-900 mt-1">{{ balance.total | number:'1.2-2' }}</p>
          </div>
          <div class="bg-white rounded-lg shadow p-6 border border-gray-200">
            <h2 class="text-sm font-medium text-gray-500">Cash</h2>
            <p class="text-2xl font-bold text-gray-900 mt-1">{{ balance.cash | number:'1.2-2' }}</p>
          </div>
          <div class="bg-white rounded-lg shadow p-6 border border-gray-200">
            <h2 class="text-sm font-medium text-gray-500">Online</h2>
            <p class="text-2xl font-bold text-gray-900 mt-1">{{ balance.online | number:'1.2-2' }}</p>
          </div>
        </div>
        <div class="flex gap-4 flex-wrap">
          <a routerLink="/wallets" class="text-primary-600 hover:underline">Manage Wallets</a>
          <a routerLink="/people" class="text-primary-600 hover:underline">People / Ledger</a>
          <a routerLink="/transactions" class="text-primary-600 hover:underline">Transactions</a>
        </div>
      }
    </div>
  `,
})
export class DashboardComponent implements OnInit {
  balance: BalanceResponse | null = null;
  loading = true;
  error = '';

  constructor(private walletService: WalletService) {}

  ngOnInit(): void {
    this.walletService.getBalance().subscribe({
      next: (b) => {
        this.balance = b;
        this.loading = false;
      },
      error: (err) => {
        this.error = err.error?.message || 'Failed to load balance.';
        this.loading = false;
      },
    });
  }
}
