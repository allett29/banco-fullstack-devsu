import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Cliente } from '../models/cliente.model';

@Injectable({ providedIn: 'root' })
export class ClienteService {
  private readonly url = `${environment.apiUrl}/clientes`;

  constructor(private readonly http: HttpClient) {}

  listar(buscar?: string): Observable<Cliente[]> {
    let params = new HttpParams();
    if (buscar) {
      params = params.set('buscar', buscar);
    }
    return this.http.get<Cliente[]>(this.url, { params });
  }

  obtener(id: number): Observable<Cliente> {
    return this.http.get<Cliente>(`${this.url}/${id}`);
  }

  crear(cliente: Cliente): Observable<Cliente> {
    return this.http.post<Cliente>(this.url, cliente);
  }

  actualizar(id: number, cliente: Cliente): Observable<Cliente> {
    return this.http.put<Cliente>(`${this.url}/${id}`, cliente);
  }

  patch(id: number, data: Partial<Cliente>): Observable<Cliente> {
    return this.http.patch<Cliente>(`${this.url}/${id}`, data);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
