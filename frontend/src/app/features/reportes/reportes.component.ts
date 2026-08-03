import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Cliente } from '../../core/models/cliente.model';
import { Reporte } from '../../core/models/reporte.model';
import { ClienteService } from '../../core/services/cliente.service';
import { ReporteService } from '../../core/services/reporte.service';

@Component({
  selector: 'app-reportes',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './reportes.component.html',
  styleUrl: './reportes.component.scss'
})
export class ReportesComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly clienteService = inject(ClienteService);
  private readonly reporteService = inject(ReporteService);

  clientes: Cliente[] = [];
  reporte: Reporte | null = null;
  mensajeError = '';

  form = this.fb.group({
    clienteId: [null as number | null, Validators.required],
    fechaInicio: ['', Validators.required],
    fechaFin: ['', Validators.required]
  });

  ngOnInit(): void {
    this.clienteService.listar().subscribe({
      next: (data) => (this.clientes = data),
      error: (err) => this.mostrarError(err)
    });
  }

  consultar(): void {
    this.mensajeError = '';
    this.reporte = null;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.mensajeError = 'Seleccione cliente y rango de fechas.';
      return;
    }

    const { clienteId, fechaInicio, fechaFin } = this.form.getRawValue();
    this.reporteService.generar(clienteId!, fechaInicio!, fechaFin!).subscribe({
      next: (data) => (this.reporte = data),
      error: (err) => this.mostrarError(err)
    });
  }

  descargarPdf(): void {
    if (!this.reporte?.pdfBase64) {
      return;
    }
    this.reporteService.descargarPdf(
      this.reporte.pdfBase64,
      `estado-cuenta-${this.reporte.clienteNombre}.pdf`
    );
  }

  campoInvalido(nombre: string): boolean {
    const control = this.form.get(nombre);
    return !!control && control.invalid && (control.dirty || control.touched);
  }

  private mostrarError(err: any): void {
    this.mensajeError = err?.error?.message || 'Ocurrió un error al generar el reporte.';
  }
}
