import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Cliente } from '../../core/models/cliente.model';
import { ClienteService } from '../../core/services/cliente.service';

@Component({
  selector: 'app-clientes',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './clientes.component.html',
  styleUrl: './clientes.component.scss'
})
export class ClientesComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly clienteService = inject(ClienteService);

  clientes: Cliente[] = [];
  busqueda = '';
  editandoId: number | null = null;
  mensajeError = '';
  mensajeOk = '';

  form = this.fb.group({
    nombre: ['', Validators.required],
    genero: ['', Validators.required],
    edad: [18, [Validators.required, Validators.min(0)]],
    identificacion: ['', Validators.required],
    direccion: ['', Validators.required],
    telefono: ['', Validators.required],
    contrasena: ['', Validators.required],
    estado: [true, Validators.required]
  });

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.clienteService.listar(this.busqueda || undefined).subscribe({
      next: (data) => (this.clientes = data),
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

    const payload = this.form.getRawValue() as Cliente;

    // Al editar, si la contraseña queda vacía se usa PATCH parcial
    // para no sobreescribirla; si se digita una nueva, PUT completo.
    let request$;
    if (!this.editandoId) {
      request$ = this.clienteService.crear(payload);
    } else if (payload.contrasena) {
      request$ = this.clienteService.actualizar(this.editandoId, payload);
    } else {
      const { contrasena, ...sinClave } = payload;
      request$ = this.clienteService.patch(this.editandoId, sinClave);
    }

    request$.subscribe({
      next: () => {
        this.mensajeOk = this.editandoId ? 'Cliente actualizado.' : 'Cliente creado.';
        this.cancelar();
        this.cargar();
      },
      error: (err) => this.mostrarError(err)
    });
  }

  editar(cliente: Cliente): void {
    this.editandoId = cliente.clienteId ?? null;
    this.form.patchValue({
      nombre: cliente.nombre,
      genero: cliente.genero,
      edad: cliente.edad,
      identificacion: cliente.identificacion,
      direccion: cliente.direccion,
      telefono: cliente.telefono,
      contrasena: '',
      estado: cliente.estado
    });
    this.form.get('contrasena')?.clearValidators();
    this.form.get('contrasena')?.updateValueAndValidity();
  }

  eliminar(cliente: Cliente): void {
    if (!cliente.clienteId || !confirm(`¿Eliminar a ${cliente.nombre}?`)) {
      return;
    }
    this.clienteService.eliminar(cliente.clienteId).subscribe({
      next: () => {
        this.mensajeOk = 'Cliente eliminado.';
        this.cargar();
      },
      error: (err) => this.mostrarError(err)
    });
  }

  cancelar(): void {
    this.editandoId = null;
    this.form.reset({ edad: 18, estado: true, contrasena: '' });
    this.form.get('contrasena')?.setValidators(Validators.required);
    this.form.get('contrasena')?.updateValueAndValidity();
  }

  campoInvalido(nombre: string): boolean {
    const control = this.form.get(nombre);
    return !!control && control.invalid && (control.dirty || control.touched);
  }

  private mostrarError(err: any): void {
    this.mensajeError = err?.error?.message || 'Ocurrió un error al procesar la solicitud.';
  }
}
