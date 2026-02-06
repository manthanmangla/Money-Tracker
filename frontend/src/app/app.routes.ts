import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'login', loadComponent: () => import('./auth/login/login.component').then(m => m.LoginComponent) },
  { path: 'register', loadComponent: () => import('./auth/register/register.component').then(m => m.RegisterComponent) },
  { path: 'dashboard', loadComponent: () => import('./dashboard/dashboard.component').then(m => m.DashboardComponent), canActivate: [authGuard] },
  { path: 'wallets', loadComponent: () => import('./wallets/wallets.component').then(m => m.WalletsComponent), canActivate: [authGuard] },
  { path: 'people', loadComponent: () => import('./people/people.component').then(m => m.PeopleComponent), canActivate: [authGuard] },
  { path: 'transactions', loadComponent: () => import('./transactions/transactions.component').then(m => m.TransactionsComponent), canActivate: [authGuard] },
  { path: '**', redirectTo: 'dashboard' },
];
