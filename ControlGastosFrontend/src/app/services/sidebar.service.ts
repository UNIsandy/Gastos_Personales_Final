import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class SidebarService {
  private isOpen = new BehaviorSubject(false);
  isOpen$ = this.isOpen.asObservable();

  toggle() { this.isOpen.next(!this.isOpen.value); }
  close() { this.isOpen.next(false); }
  open() { this.isOpen.next(true); }
}
