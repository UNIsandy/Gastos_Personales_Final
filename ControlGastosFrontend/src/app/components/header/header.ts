import { Component, OnInit } from '@angular/core';
import { SidebarService } from '../../services/sidebar.service';

@Component({
  selector: 'app-header',
  imports: [],
  templateUrl: './header.html',
  styleUrl: './header.scss'
})
export class HeaderComponent implements OnInit {
  nombre = '';
  inicial = '';

  constructor(public sidebar: SidebarService) {}

  ngOnInit() {
    const data = localStorage.getItem('user_data');
    if (data) {
      const u = JSON.parse(data);
      this.nombre = u.nombre || (u.email ? u.email.split('@')[0] : 'Usuario');
      this.inicial = this.nombre.charAt(0).toUpperCase();
    }
  }
}
