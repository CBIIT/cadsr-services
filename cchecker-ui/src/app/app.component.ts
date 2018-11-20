import { Component } from '@angular/core';
import { directiveCreate } from '../../node_modules/@angular/core/src/render3/instructions';
import { PropertyBindingType } from '../../node_modules/@angular/compiler';
import { environment } from '../environments/environment';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'cchecker-ui';
  formBuilderUrl:string = environment.formBuilderUrl;
  cdeBrowserUrl:string = environment.cdeBrowserUrl;
}

