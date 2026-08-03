import { Routes } from '@angular/router';
import { ShellComponent } from './layout/shell/shell.component';

export const routes: Routes = [
  {
    path: '',
    component: ShellComponent,
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'clientes' },
      {
        path: 'clientes',
        loadComponent: () =>
          import('./features/clientes/clientes.component').then((m) => m.ClientesComponent)
      },
      {
        path: 'cuentas',
        loadComponent: () =>
          import('./features/cuentas/cuentas.component').then((m) => m.CuentasComponent)
      },
      {
        path: 'movimientos',
        loadComponent: () =>
          import('./features/movimientos/movimientos.component').then((m) => m.MovimientosComponent)
      },
      {
        path: 'reportes',
        loadComponent: () =>
          import('./features/reportes/reportes.component').then((m) => m.ReportesComponent)
      }
    ]
  },
  { path: '**', redirectTo: 'clientes' }
];
