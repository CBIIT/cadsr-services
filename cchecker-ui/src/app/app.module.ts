import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import { AlsFormComponent } from './als-form/als-form.component';
import { AlsFormListComponent } from './als-form-list/als-form-list.component';
import { AlsUploadFormComponent } from './als-upload-form/als-upload-form.component';
import { AlsReportComponent } from './als-report/als-report.component';
import { NavigationComponent } from './navigation/navigation.component';

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
    BrowserModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
