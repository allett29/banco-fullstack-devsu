import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Cuenta } from '../../core/models/cuenta.model';
import { Movimiento } from '../../core/models/movimiento.model';
import { CuentaService } from '../../core/services/cuenta.service';
import { MovimientoService } from '../../core/services/movimiento.service';

@Component({
  selector: 'app-movimientos',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './movimientos.component.html',
  styleUrl: './movimientos.component.scss'
})
export class MovimientosComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly movimientoService = inject(MovimientoService);
  private readonly cuentaService = inject(CuentaService);

  movimientos: Movimiento[] = [];
  cuentas: Cuenta[] = [];
  busqueda = '';
  mensajeError = '';
  mensajeOk = '';

  form = this.fb.group({
    tipoMovimiento: ['CREDITO', Validators.required],
    valor: [0, [Validators.required, Validators.min(0.01)]],
    cuentaId: [null as number | null, Validators.required]
  });

  ngOnInit(): void {
    this.cuentaService.listar().subscribe({
      next: (data) => (this.cuentas = data),
      error: (err) => this.mostrarError(err)
    });
    this.cargar();
  }

  cargar(): void {
    this.movimientoService.listar(this.busqueda || undefined).subscribe({
      next: (data) => (this.movimientos = data),
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

    const payload = this.form.getRawValue() as Movimiento;
    this.movimientoService.crear(payload).subscribe({
      next: () => {
        this.mensajeOk = 'Movimiento registrado.';
        this.form.reset({ tipoMovimiento: 'CREDITO', valor: 0, cuentaId: null });
        this.cargar();
        this.cuentaService.listar().subscribe((data) => (this.cuentas = data));
      },
      error: (err) => this.mostrarError(err)
    });
  }

  eliminar(movimiento: Movimiento): void {
    if (!movimiento.id || !confirm('¿Eliminar este movimiento?')) {
      return;
    }
    this.movimientoService.eliminar(movimiento.id).subscribe({
      next: () => {
        this.mensajeOk = 'Movimiento eliminado.';
        this.cargar();
      },
      error: (err) => this.mostrarError(err)
    });
  }

  campoInvalido(nombre: string): boolean {
    const control = this.form.get(nombre);
    return !!control && control.invalid && (control.dirty || control.touched);
  }

  private mostrarError(err: any): void {
    this.mensajeError = err?.error?.message || 'Ocurrió un error al procesar la solicitud.';
  }
}
