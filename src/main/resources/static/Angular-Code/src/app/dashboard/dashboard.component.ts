import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { JwtService } from '../jwt.service';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { catchError, map, of, pipe } from 'rxjs';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent {

  allJournals: any = [];

  constructor(private router: Router, private jwtService: JwtService, private http: HttpClient) { }

  getJournals() {
    this.http.get('http://localhost:8080/journal').pipe(
      map((response: any) => {
        this.allJournals = response;
        console.log(this.allJournals);
      }),
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401) {
          console.log("hihi");
        }
        if (error.status === 403) {
          // this.router.navigate(['/forbidden']);
        }
        if (error.status === 404) { 
          // this.router.navigate(['/not-found']);
        }
        if (error.status === 500) {
          // this.router.navigate(['/server-error']);
        }

        return of([]);
      })
    ).subscribe();
  }

}