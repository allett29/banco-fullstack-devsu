import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Cuenta } from '../models/cuenta.model';

@Injectable({ providedIn: 'root' })
export class CuentaService {
  private readonly url = `${environment.apiUrl}/cuentas`;

  constructor(private readonly http: HttpClient) {}

  listar(buscar?: string): Observable<Cuenta[]> {
    let params = new HttpParams();
    if (buscar) {
      params = params.set('buscar', buscar);
    }
    return this.http.get<Cuenta[]>(this.url, { params });
  }

  obtener(id: number): Observable<Cuenta> {
    return this.http.get<Cuenta>(`${this.url}/${id}`);
  }

  crear(cuenta: Cuenta): Observable<Cuenta> {
    return this.http.post<Cuenta>(this.url, cuenta);
  }

  actualizar(id: number, cuenta: Cuenta): Observable<Cuenta> {
    return this.http.put<Cuenta>(`${this.url}/${id}`, cuenta);
  }

  patch(id: number, data: Partial<Cuenta>): Observable<Cuenta> {
    return this.http.patch<Cuenta>(`${this.url}/${id}`, data);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
