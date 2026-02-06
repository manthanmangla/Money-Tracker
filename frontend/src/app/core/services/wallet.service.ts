import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export type WalletType = 'CASH' | 'ONLINE';

export interface WalletResponse {
  id: number;
  type: WalletType;
  balance: number;
}

export interface BalanceResponse {
  cash: number;
  online: number;
  total: number;
}

@Injectable({ providedIn: 'root' })
export class WalletService {
  private api = `${environment.apiUrl}/api/wallets`;

  constructor(private http: HttpClient) {}

  list(): Observable<WalletResponse[]> {
    return this.http.get<WalletResponse[]>(this.api);
  }

  getBalance(): Observable<BalanceResponse> {
    return this.http.get<BalanceResponse>(`${this.api}/balance`);
  }

  create(type: WalletType): Observable<WalletResponse> {
    return this.http.post<WalletResponse>(this.api, { type });
  }
}
