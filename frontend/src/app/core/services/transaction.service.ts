import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export type TransactionType = 'RECEIVED' | 'GIVEN' | 'EXPENSE' | 'INCOME' | 'TRANSFER';
export type WalletType = 'CASH' | 'ONLINE';

export interface TransactionResponse {
  id: number;
  personId: number | null;
  fromWalletId: number | null;
  toWalletId: number | null;
  amount: number;
  transactionType: TransactionType;
  description: string | null;
  date: string;
  createdAt: string;
}

export interface CreateTransactionRequest {
  personId?: number | null;
  fromWalletId?: number | null;
  toWalletId?: number | null;
  amount: number;
  transactionType: TransactionType;
  description?: string | null;
  date?: string | null;
}

@Injectable({ providedIn: 'root' })
export class TransactionService {
  private api = `${environment.apiUrl}/api/transactions`;

  constructor(private http: HttpClient) {}

  list(params?: { wallet?: WalletType; type?: TransactionType; from?: string; to?: string }): Observable<TransactionResponse[]> {
    let httpParams = new HttpParams();
    if (params?.wallet) httpParams = httpParams.set('wallet', params.wallet);
    if (params?.type) httpParams = httpParams.set('type', params.type);
    if (params?.from) httpParams = httpParams.set('from', params.from);
    if (params?.to) httpParams = httpParams.set('to', params.to);
    return this.http.get<TransactionResponse[]>(this.api, { params: httpParams });
  }

  create(body: CreateTransactionRequest): Observable<TransactionResponse> {
    return this.http.post<TransactionResponse>(this.api, body);
  }

  reverse(id: number): Observable<TransactionResponse> {
    return this.http.post<TransactionResponse>(`${this.api}/${id}/reverse`, {});
  }
}
