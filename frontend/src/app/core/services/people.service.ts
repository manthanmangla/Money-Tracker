import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface PersonSummary {
  id: number;
  name: string;
  phone: string | null;
  notes: string | null;
  totalReceived: number;
  totalGiven: number;
  netBalance: number;
  status: 'THEY_OWE_ME' | 'I_OWE_THEM' | 'SETTLED';
}

@Injectable({ providedIn: 'root' })
export class PeopleService {
  private api = `${environment.apiUrl}/api/people`;

  constructor(private http: HttpClient) {}

  list(): Observable<PersonSummary[]> {
    return this.http.get<PersonSummary[]>(this.api);
  }

  getLedger(id: number): Observable<PersonSummary> {
    return this.http.get<PersonSummary>(`${this.api}/${id}/ledger`);
  }

  create(name: string, phone?: string, notes?: string): Observable<PersonSummary> {
    return this.http.post<PersonSummary>(this.api, { name, phone: phone || null, notes: notes || null });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/${id}`);
  }
}
