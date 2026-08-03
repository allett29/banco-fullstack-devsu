export interface ReporteMovimiento {
  Fecha: string;
  Cliente: string;
  'Numero Cuenta': string;
  Tipo: string;
  'Saldo Inicial': number;
  Estado: boolean;
  Movimiento: number;
  'Saldo Disponible': number;
}

export interface Reporte {
  clienteId: number;
  clienteNombre: string;
  totalDebitos: number;
  totalCreditos: number;
  movimientos: ReporteMovimiento[];
  pdfBase64: string;
}
