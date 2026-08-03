import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { Cliente } from '../models/cliente.model';
import { ClienteService } from './cliente.service';

describe('ClienteService', () => {
  let service: ClienteService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule]
    });
    service = TestBed.inject(ClienteService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('debe listar clientes', () => {
    const mock: Cliente[] = [
      {
        clienteId: 1,
        nombre: 'Jose Lema',
        genero: 'Masculino',
        edad: 30,
        identificacion: '1001001001',
        direccion: 'Otavalo sn y principal',
        telefono: '098254785',
        estado: true
      }
    ];

    service.listar().subscribe((data) => {
      expect(data.length).toBe(1);
      expect(data[0].nombre).toBe('Jose Lema');
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/clientes`);
    expect(req.request.method).toBe('GET');
    req.flush(mock);
  });

  it('debe enviar parámetro de búsqueda', () => {
    service.listar('Jose').subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/clientes?buscar=Jose`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });
});
