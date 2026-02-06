import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="max-w-md mx-auto">
      <h1 class="text-2xl font-bold text-gray-900 mb-6">Login</h1>
      @if (errorMessage) {
        <div class="mb-4 p-3 rounded-md bg-red-50 text-red-700 text-sm">{{ errorMessage }}</div>
      }
      <form [formGroup]="form" (ngSubmit)="onSubmit()" class="space-y-4">
        <div>
          <label for="email" class="block text-sm font-medium text-gray-700 mb-1">Email</label>
          <input id="email" type="email" formControlName="email" autocomplete="email"
            class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-primary-500 focus:ring-1 focus:ring-primary-500" />
          @if (form.get('email')?.invalid && form.get('email')?.touched) {
            <p class="mt-1 text-sm text-red-600">Valid email required</p>
          }
        </div>
        <div>
          <label for="password" class="block text-sm font-medium text-gray-700 mb-1">Password</label>
          <input id="password" type="password" formControlName="password" autocomplete="current-password"
            class="w-full rounded-md border border-gray-300 px-3 py-2 focus:border-primary-500 focus:ring-1 focus:ring-primary-500" />
          @if (form.get('password')?.invalid && form.get('password')?.touched) {
            <p class="mt-1 text-sm text-red-600">Password required</p>
          }
        </div>
        <button type="submit" [disabled]="form.invalid || loading"
          class="w-full bg-primary-600 text-white py-2 px-4 rounded-md hover:bg-primary-700 disabled:opacity-50 disabled:cursor-not-allowed">
          @if (loading) { Logging in… } @else { Login }
        </button>
      </form>
      <p class="mt-4 text-sm text-gray-600">
        Don't have an account? <a routerLink="/register" class="text-primary-600 hover:underline">Register</a>
      </p>
    </div>
  `,
})
export class LoginComponent {
  form: FormGroup;
  loading = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router
  ) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required],
    });
  }

  onSubmit(): void {
    if (this.form.invalid || this.loading) return;
    this.loading = true;
    this.errorMessage = '';
    this.auth.login(this.form.value.email, this.form.value.password).subscribe({
      next: () => this.router.navigate(['/dashboard']),
      error: (err) => {
        this.loading = false;
        const msg = err.error?.message;
        this.errorMessage = msg || (err.status === 401 ? 'Invalid email or password.' : (err.status === 0 ? 'Cannot reach server. Is the backend running on port 8081?' : 'Login failed. Try again.'));
      },
    });
  }
}
