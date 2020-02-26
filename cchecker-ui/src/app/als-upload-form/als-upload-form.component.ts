import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { RestService } from '../services/rest.service';
import { HttpEventType }  from '@angular/common/http';
import { FormListService } from '../services/formlist.service';
import { Observable } from 'rxjs';

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
  alsFile:FormData;
  name:String;
  isValidating: Boolean;
  isGeneratingXML: Boolean;
  fileName:String;

  constructor(private router:Router, private restService:RestService, private formListService:FormListService) { 
  }
  
  // always keeps current file as File object //
  getFile = event => {
    this.file = new FormData();
    const tFile = event.target.files[0];
    this.file.append('file', tFile, tFile.name);
    this.fileName=tFile.name;
  };
  
  // user clicked submit validate form fields are valid and upload //
  submitForm(error_name, error_file, event) {
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
        this.errorMessage = 'Cannot communicate with the server. <br /><br />If this continues to fail report to <a href="/contact-us">NCI Application Support Desk</a>.'
        this.uploadProgress = 0;
        if (error.error=='' || typeof(error.error)=='object') {
          this.errorMessage = 'Cannot communicate with the server. <br /><br />If this continues to fail report to <a href="/contact-us">NCI Application Support Desk</a>.'
        }
        else {
          this.errorMessage = error.error + '<br /><br />If this continues to fail report to <a href="/contact-us">NCI Application Support Desk</a>.';
        }
      },
      () => {
        sessionStorage['name']=this.name, sessionStorage['file']=this.fileName;
        this.router.navigateByUrl('/forms')
      });
  };

  ngOnInit() {  
    this.isValidating = false;
    if (this.formListService.getValidationStatus()) {
      this.isValidating = true;
    };
    this.isGeneratingXML = this.formListService.getSessionDataItem('isGeneratingXML');  
  }

}
