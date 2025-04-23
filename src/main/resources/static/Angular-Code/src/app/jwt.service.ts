import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";

@Injectable({
  providedIn: 'root'
})
export class JwtService {

  private jwtToken: string | null = null;

  constructor(private http: HttpClient) {
    this.jwtToken = localStorage.getItem('jwt');
  }

  validateToken() {
    return this.http.get('http://localhost:8080/user/validateJwt', {
      headers: { 'Authorization': `Bearer ${this.jwtToken}` }
    });
  }

  setToken(token: string): void {
    this.jwtToken = token;
    localStorage.setItem('jwt', token);
  }

  getToken(): string | null {
    return this.jwtToken;
  }

  removeToken(): void {
    this.jwtToken = null;
    localStorage.removeItem('jwt');
  }
}