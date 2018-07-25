import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {Routes,  RouterModule} from '@angular/router';
import { AppComponent } from './app.component';
import { AlsFormListComponent } from './als-form-list/als-form-list.component';
import { AlsUploadFormComponent } from './als-upload-form/als-upload-form.component';
import { AlsReportComponent } from './als-report/als-report.component';
import { NavigationComponent } from './navigation/navigation.component';
import { HttpClientModule } from '@angular/common/http';

const APP_ROUTES: Routes = [
  // Default empty path
  { path: 'forms', component: AlsFormListComponent },
  { path: 'report', component: AlsReportComponent },
  { path: '', component: AlsUploadFormComponent }
];

@NgModule({
  declarations: [
    AppComponent,
    AlsFormListComponent,
    AlsUploadFormComponent,
    AlsReportComponent,
    NavigationComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    FormsModule,
    RouterModule.forRoot(APP_ROUTES)
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
