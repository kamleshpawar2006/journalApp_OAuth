import { Routes } from '@angular/router';
import { Oauth2RedirectComponent } from './oauth2-redirect/oauth2-redirect.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { AuthGuard } from './AuthGuard.guard';
import { LoginComponent } from './login/login.component';

export const routes: Routes = [
    { path: 'oauth2/redirect', component: Oauth2RedirectComponent },
    { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard] },
    { path: 'login', component: LoginComponent },
    { path: '', redirectTo: '/login', pathMatch: 'full' },
];
