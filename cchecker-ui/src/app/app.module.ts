import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import {Routes,  RouterModule} from '@angular/router';
import { AppComponent } from './app.component';
import { AlsFormComponent } from './als-form/als-form.component';
import { AlsFormListComponent } from './als-form-list/als-form-list.component';
import { AlsUploadFormComponent } from './als-upload-form/als-upload-form.component';
import { AlsReportComponent } from './als-report/als-report.component';
import { NavigationComponent } from './navigation/navigation.component';

const APP_ROUTES: Routes = [
  // Default empty path
  { path: 'forms', component: AlsFormListComponent },
  { path: 'report', component: AlsReportComponent },
  { path: '', component: AlsUploadFormComponent }
];

@NgModule({
  declarations: [
    AppComponent,
    AlsFormComponent,
    AlsFormListComponent,
    AlsUploadFormComponent,
    AlsReportComponent,
    NavigationComponent
  ],
  imports: [
    BrowserModule,
    RouterModule.forRoot(APP_ROUTES)
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
