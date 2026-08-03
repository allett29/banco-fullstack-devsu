export interface Cuenta {
  id?: number;
  numeroCuenta: string;
  tipoCuenta: string;
  saldoInicial: number;
  saldoActual?: number;
  estado: boolean;
  clienteId: number;
  clienteNombre?: string;
}
