import { Component, OnInit } from '@angular/core';
import { Router, NavigationStart, ActivatedRoute } from '../../../node_modules/@angular/router';

@Component({
  selector: 'app-navigation',
  templateUrl: './navigation.component.html',
  styleUrls: ['./navigation.component.css']
})
export class NavigationComponent implements OnInit {
  currentRoute:string;
  constructor(private router:Router, activatedRoute:ActivatedRoute) { 
    this.currentRoute = '/';
    router.events.subscribe(d => d instanceof NavigationStart ? this.currentRoute = d.url : false);
  }
ngOnInit() {
}

getSelectedStyle = (link,curreRoute) => link==this.currentRoute? true : false;

}
