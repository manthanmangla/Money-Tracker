import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

const TOKEN_KEY = 'money_tracker_token';

export interface AuthResponse {
  userId: number;
  email: string;
  token: string | null;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private api = `${environment.apiUrl}/api/auth`;

  constructor(private http: HttpClient, private router: Router) {}

  register(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.api}/register`, { email, password }).pipe(
      tap((res) => { if (res.token) this.setToken(res.token); })
    );
  }

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.api}/login`, { email, password }).pipe(
      tap((res) => { if (res.token) this.setToken(res.token); })
    );
  }

  setToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  getEmail(): string | null {
    const t = this.getToken();
    if (!t) return null;
    try {
      const payload = JSON.parse(atob(t.split('.')[1]));
      return payload.email ?? null;
    } catch {
      return null;
    }
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    this.router.navigate(['/login']);
  }
}
