import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { JwtService } from './jwt.service';
import { catchError, map, Observable, of } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(private router: Router, private jwtService: JwtService) {}

  canActivate(): Observable<boolean> {
    const token = this.jwtService.getToken();

    if (!token) {
      this.router.navigate(['/login']);
      return of(false);
    }

    return this.jwtService.validateToken().pipe(
      map(response => {
        return true;
      }),
      catchError(error => {
        this.jwtService.removeToken();
        this.router.navigate(['/login']);
        return of(false);
      })
    );
  }

}
