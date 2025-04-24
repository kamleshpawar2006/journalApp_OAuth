import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { JwtService } from './jwt.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'journal-spa';
  profilePicture: any = null;
  tokenExpiredMessage: string | null = null;
  
  constructor(private jwtService: JwtService) {
    
  }

  ngOnInit(): void {
    this.jwtService.getProfilePictureObservable().subscribe((pic) => {
      console.log(pic);
      if (pic) {
        this.profilePicture = pic;
        console.log('Profile Picture:', this.profilePicture);
      }
    });

    this.jwtService.getRefreshTokenExpired().subscribe((expired) => {
      if (expired) {
        this.tokenExpiredMessage = 'Refresh token expired. Please log in again.';
        console.log('Refresh token expired');
        this.profilePicture = null;
      }
    });

  }
  
}
