import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { RestService } from '../services/rest.service';
import { HttpEventType }  from '@angular/common/http';
import { EventHandlerVars } from '@angular/compiler/src/compiler_util/expression_converter';
import { DataService } from '../services/data.service';

@Component({
  selector: 'app-als-upload-form',
  templateUrl: './als-upload-form.component.html',
  styleUrls: ['./als-upload-form.component.css']
})
export class AlsUploadFormComponent implements OnInit {
  error:boolean;
  errorMessage:String;
  file:FormData;
  submitted:boolean;
  uploadProgress:Number;

  constructor(private router:Router, private restService:RestService, private dataService:DataService) { 
    this.submitted = false;
    this.uploadProgress = 0;
    this.error=false;
  }
  // always keeps current file as File object //
  getFile = event => {
    this.file = new FormData();
    const file = event.target.files[0];
    this.file.append('file', file, file.name);
  };
  
  // user clicked submit //
  // validate form fields are valid and upload //
  submitForm(error_name, error_file) {
    this.error = false; // reset error //
    this.uploadProgress = 0;
    this.submitted = true; // set for form validation //
    if (error_name.valid && error_file.valid) { // check form fields //
      this.uploadFile(error_file); // upload file to server //
    };
    return false;
  };

  // submit name, file to server for processing //
  uploadFile = formData=>  {
    this.restService.uploadAlsFile(this.file).subscribe(
      e => {
        if (e.type === HttpEventType.UploadProgress) {
            this.uploadProgress = Math.round((e.loaded/e.total)*100)
        };
      }, 
      error => { this.errorMessage = event.target['response']; this.error = true },
      () => {
        this.dataService.storeLocalFormListData(JSON.parse(event.target['response']));
        this.router.navigateByUrl('/forms')
      });
  };

  ngOnInit() {  
  }

}
