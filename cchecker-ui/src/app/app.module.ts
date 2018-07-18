import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';


import { AppComponent } from './app.component';
import { FormListComponent } from './form-list/form-list.component';
import { FormComponent } from './form/form.component';
import { UploadFormComponent } from './upload-form/upload-form.component';
import { ReportComponent } from './report/report.component';


@NgModule({
  declarations: [
    AppComponent,
    FormListComponent,
    FormComponent,
    UploadFormComponent,
    ReportComponent
  ],
  imports: [
    BrowserModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
