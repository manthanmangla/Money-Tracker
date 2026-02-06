import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from './core/services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <nav class="bg-white shadow border-b border-gray-200">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-14">
          <div class="flex items-center gap-6">
            <a routerLink="/dashboard" routerLinkActive="font-semibold" class="text-gray-700 hover:text-primary-600">Money Tracker</a>
            @if (auth.isLoggedIn()) {
              <a routerLink="/dashboard" routerLinkActive="font-semibold" class="text-gray-600 hover:text-primary-600">Dashboard</a>
              <a routerLink="/wallets" routerLinkActive="font-semibold" class="text-gray-600 hover:text-primary-600">Wallets</a>
              <a routerLink="/people" routerLinkActive="font-semibold" class="text-gray-600 hover:text-primary-600">People</a>
              <a routerLink="/transactions" routerLinkActive="font-semibold" class="text-gray-600 hover:text-primary-600">Transactions</a>
            }
          </div>
          <div class="flex items-center gap-4">
            @if (auth.isLoggedIn()) {
              <span class="text-sm text-gray-500">{{ auth.getEmail() }}</span>
              <button (click)="logout()" class="text-sm text-red-600 hover:text-red-700">Logout</button>
            } @else {
              <a routerLink="/login" class="text-sm text-gray-600 hover:text-primary-600">Login</a>
              <a routerLink="/register" class="text-sm bg-primary-600 text-white px-3 py-1.5 rounded hover:bg-primary-700">Register</a>
            }
          </div>
        </div>
      </div>
    </nav>
    <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <router-outlet></router-outlet>
    </main>
  `,
})
export class AppComponent {
  constructor(public auth: AuthService) {}

  logout(): void {
    this.auth.logout();
  }
}
