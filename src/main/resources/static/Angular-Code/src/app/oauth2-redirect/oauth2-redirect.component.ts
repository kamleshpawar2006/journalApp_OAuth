import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-oauth2-redirect',
  template: `<p>Redirecting...</p>`,
})
export class Oauth2RedirectComponent implements OnInit {
  constructor(private router: Router) {}

  ngOnInit(): void {
    const params = new URLSearchParams(window.location.search);
    const token = params.get('token');

    if (token) {
      // Store the token in localStorage (or cookie, your choice)
      localStorage.setItem('jwt', token);

      // ✅ Redirect to a protected area (like dashboard)
      this.router.navigate(['/dashboard']);
    } else {
      // Handle error or fallback
      this.router.navigate(['/login']);
    }
  }
}
