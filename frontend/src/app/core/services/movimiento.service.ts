import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Movimiento } from '../models/movimiento.model';

@Injectable({ providedIn: 'root' })
export class MovimientoService {
  private readonly url = `${environment.apiUrl}/movimientos`;

  constructor(private readonly http: HttpClient) {}

  listar(buscar?: string): Observable<Movimiento[]> {
    let params = new HttpParams();
    if (buscar) {
      params = params.set('buscar', buscar);
    }
    return this.http.get<Movimiento[]>(this.url, { params });
  }

  obtener(id: number): Observable<Movimiento> {
    return this.http.get<Movimiento>(`${this.url}/${id}`);
  }

  crear(movimiento: Movimiento): Observable<Movimiento> {
    return this.http.post<Movimiento>(this.url, movimiento);
  }

  actualizar(id: number, movimiento: Movimiento): Observable<Movimiento> {
    return this.http.put<Movimiento>(`${this.url}/${id}`, movimiento);
  }

  patch(id: number, data: Partial<Movimiento>): Observable<Movimiento> {
    return this.http.patch<Movimiento>(`${this.url}/${id}`, data);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
