import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Cliente } from '../../core/models/cliente.model';
import { Cuenta } from '../../core/models/cuenta.model';
import { ClienteService } from '../../core/services/cliente.service';
import { CuentaService } from '../../core/services/cuenta.service';

@Component({
  selector: 'app-cuentas',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './cuentas.component.html',
  styleUrl: './cuentas.component.scss'
})
export class CuentasComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly cuentaService = inject(CuentaService);
  private readonly clienteService = inject(ClienteService);

  cuentas: Cuenta[] = [];
  clientes: Cliente[] = [];
  busqueda = '';
  editandoId: number | null = null;
  mensajeError = '';
  mensajeOk = '';

  form = this.fb.group({
    numeroCuenta: ['', Validators.required],
    tipoCuenta: ['Ahorros', Validators.required],
    saldoInicial: [0, [Validators.required, Validators.min(0)]],
    estado: [true, Validators.required],
    clienteId: [null as number | null, Validators.required]
  });

  ngOnInit(): void {
    this.clienteService.listar().subscribe({
      next: (data) => (this.clientes = data),
      error: (err) => this.mostrarError(err)
    });
    this.cargar();
  }

  cargar(): void {
    this.cuentaService.listar(this.busqueda || undefined).subscribe({
      next: (data) => (this.cuentas = data),
      error: (err) => this.mostrarError(err)
    });
  }

  buscar(valor: string): void {
    this.busqueda = valor;
    this.cargar();
  }

  guardar(): void {
    this.mensajeError = '';
    this.mensajeOk = '';

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.mensajeError = 'Complete los campos obligatorios.';
      return;
    }

    const payload = this.form.getRawValue() as Cuenta;
    const request$ = this.editandoId
      ? this.cuentaService.actualizar(this.editandoId, payload)
      : this.cuentaService.crear(payload);

    request$.subscribe({
      next: () => {
        this.mensajeOk = this.editandoId ? 'Cuenta actualizada.' : 'Cuenta creada.';
        this.cancelar();
        this.cargar();
      },
      error: (err) => this.mostrarError(err)
    });
  }

  editar(cuenta: Cuenta): void {
    this.editandoId = cuenta.id ?? null;
    this.form.patchValue({
      numeroCuenta: cuenta.numeroCuenta,
      tipoCuenta: cuenta.tipoCuenta,
      saldoInicial: cuenta.saldoInicial,
      estado: cuenta.estado,
      clienteId: cuenta.clienteId
    });
  }

  eliminar(cuenta: Cuenta): void {
    if (!cuenta.id || !confirm(`¿Eliminar la cuenta ${cuenta.numeroCuenta}?`)) {
      return;
    }
    this.cuentaService.eliminar(cuenta.id).subscribe({
      next: () => {
        this.mensajeOk = 'Cuenta eliminada.';
        this.cargar();
      },
      error: (err) => this.mostrarError(err)
    });
  }

  cancelar(): void {
    this.editandoId = null;
    this.form.reset({ tipoCuenta: 'Ahorros', saldoInicial: 0, estado: true, clienteId: null });
  }

  campoInvalido(nombre: string): boolean {
    const control = this.form.get(nombre);
    return !!control && control.invalid && (control.dirty || control.touched);
  }

  private mostrarError(err: any): void {
    this.mensajeError = err?.error?.message || 'Ocurrió un error al procesar la solicitud.';
  }
}
