import { Routes } from '@angular/router';
import { HomeComponent } from './pages/home/home';
import { ListaComponent } from './pages/lista/lista';
import { FormularioComponent } from './pages/formulario/formulario';
import { DashboardComponent } from './pages/dashboard/dashboard';
import { MetasComponent } from './pages/metas/metas';
import { LoginComponent } from './pages/login/login';
import { RegistroComponent } from './pages/registro/registro';
import { ProgramadasComponent } from './pages/programadas/programadas';
import { AuthGuard } from './guards/auth.guard';

export const routes: Routes = [
    { path: 'login', component: LoginComponent },
    { path: 'registro', component: RegistroComponent },
    { path: '', redirectTo: '/home', pathMatch: 'full' },
    { path: 'home', component: HomeComponent, canActivate: [AuthGuard] },
    { path: 'lista', component: ListaComponent, canActivate: [AuthGuard] },
    { path: 'formulario', component: FormularioComponent, canActivate: [AuthGuard] },
    { path: 'formulario/:id', component: FormularioComponent, canActivate: [AuthGuard] },
    { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard] },
    { path: 'metas', component: MetasComponent, canActivate: [AuthGuard] },
    { path: 'programadas', component: ProgramadasComponent, canActivate: [AuthGuard] },
    { path: '**', redirectTo: '/home' }
];
