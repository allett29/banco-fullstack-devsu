import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { ReporteService } from './reporte.service';

describe('ReporteService', () => {
  let service: ReporteService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });
    service = TestBed.inject(ReporteService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('debe consultar reporte con filtros', () => {
    service.generar(2, '2022-02-01', '2022-02-28').subscribe((data) => {
      expect(data.clienteId).toBe(2);
    });

    const req = httpMock.expectOne(
      `${environment.apiUrl}/reportes?clienteId=2&fechaInicio=2022-02-01&fechaFin=2022-02-28`
    );
    expect(req.request.method).toBe('GET');
    req.flush({
      clienteId: 2,
      clienteNombre: 'Marianela Montalvo',
      totalDebitos: 540,
      totalCreditos: 600,
      movimientos: [],
      pdfBase64: 'AAA'
    });
  });
});
