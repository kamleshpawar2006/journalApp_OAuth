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
  }
  
}
