import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Reporte } from '../models/reporte.model';

@Injectable({ providedIn: 'root' })
export class ReporteService {
  private readonly url = `${environment.apiUrl}/reportes`;

  constructor(private readonly http: HttpClient) {}

  generar(clienteId: number, fechaInicio: string, fechaFin: string): Observable<Reporte> {
    const params = new HttpParams()
      .set('clienteId', clienteId)
      .set('fechaInicio', fechaInicio)
      .set('fechaFin', fechaFin);
    return this.http.get<Reporte>(this.url, { params });
  }

  descargarPdf(base64: string, nombre = 'estado-cuenta.pdf'): void {
    const byteCharacters = atob(base64);
    const byteNumbers = Array.from(byteCharacters, (char) => char.charCodeAt(0));
    const blob = new Blob([new Uint8Array(byteNumbers)], { type: 'application/pdf' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = nombre;
    link.click();
    URL.revokeObjectURL(link.href);
  }
}
