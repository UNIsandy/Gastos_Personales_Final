import { Component, HostListener } from '@angular/core';
import { RouterLink, RouterLinkActive, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';
import { SidebarService } from '../../services/sidebar.service';

@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, RouterLinkActive, CommonModule],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.scss'
})
export class SidebarComponent {
  isOpen = false;

  constructor(private auth: AuthService, private router: Router, public sidebar: SidebarService) {
    this.sidebar.isOpen$.subscribe(v => this.isOpen = v);
  }

  @HostListener('document:click', ['$event'])
  onDocClick(e: MouseEvent) {
    const target = e.target as HTMLElement;
    if (!target.closest('.sidebar') && !target.closest('.header__menu-btn')) {
      this.sidebar.close();
    }
  }

  logout() {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
