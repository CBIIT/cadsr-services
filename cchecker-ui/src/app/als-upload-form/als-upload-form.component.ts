import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { RestService } from '../services/rest.service';
import { HttpEventType }  from '@angular/common/http';
import { FormListService } from '../services/formlist.service';

@Component({
  selector: 'app-als-upload-form',
  templateUrl: './als-upload-form.component.html',
  styleUrls: ['./als-upload-form.component.css']
})
export class AlsUploadFormComponent implements OnInit {
  errorMessage:String;
  file:FormData=null;
  submitted:boolean;
  uploadProgress:Number=0;
  name:String;

  constructor(private router:Router, private restService:RestService, private formListService:FormListService) { 
  }
  
  // always keeps current file as File object //
  getFile = event => {
    this.file = new FormData();
    const tFile = event.target.files[0];
    this.file.append('file', tFile, tFile.name);
  };
  
  // user clicked submit validate form fields are valid and upload //
  submitForm(error_name, error_file) {
    this.errorMessage = null;
    this.uploadProgress = 0;
    this.submitted = true; // set for form validation //
    if ((error_name.valid && error_file.valid) || (error_name.valid && this.file)) { // check form fields. checking for this.file because edge is broken // 
      this.uploadFile(); // upload file to server //
    };
    return false;
  };

  // submit name, file to server for processing //
  uploadFile = () =>  {
    this.restService.uploadAlsFile(this.file, this.name).subscribe(
      e => {
        if (e.type === HttpEventType.Response) {
          this.formListService.setFormListData(e.body);
        }
        else if (e.type === HttpEventType.UploadProgress) {
            this.uploadProgress = Math.round((e.loaded/e.total)*100)
        };
      }, 
      error => { 
        this.errorMessage = 'Cannot communicate with the server'
        this.uploadProgress = 0;
        if (event.target['response']=='') {
          this.errorMessage = 'Cannot communicate with the server'
        }
        else {
          this.errorMessage = event.target['response']; 
        }
      },
      () => {
        this.router.navigateByUrl('/forms')
      });
  };

  ngOnInit() {  
  }

}
