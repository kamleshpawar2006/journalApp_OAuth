import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { JwtService } from '../jwt.service';
import { catchError, map, of } from 'rxjs';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {

  constructor(private router: Router, private jwtService: JwtService) {}

  ngOnInit(): void {
    
    const params = new URLSearchParams(window.location.search);
    const token = params.get('token');
    if(token) {
      this.jwtService.setToken(token);
      this.router.navigate(['/dashboard']);
    } else {
      this.jwtService.validateToken().pipe(
        map((response: any) => {
          console.log(response);
          this.jwtService.setProfilePicture(response['profileImage']);
          this.router.navigate(['/dashboard']);
        }),
        catchError(error => {
          console.log(error);
          this.jwtService.removeToken();
          this.router.navigate(['/login']);
          return of(null);
        })
      );
    }
  }

  loginWithGoogle(): void {
    window.location.href = 'http://localhost:8080/oauth2/authorization/google';
  }

}
