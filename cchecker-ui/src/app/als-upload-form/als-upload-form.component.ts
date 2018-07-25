import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { RestService } from '../services/rest.service';

@Component({
  selector: 'app-als-upload-form',
  templateUrl: './als-upload-form.component.html',
  styleUrls: ['./als-upload-form.component.css']
})
export class AlsUploadFormComponent implements OnInit {
  name:string;
  alsFile:File;
  submitted:boolean;
  file:FormData;

  constructor(private router:Router, private restService:RestService) { 
    this.submitted = false;
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
    this.submitted = true; // set for form validation //
    if (error_name.valid && error_file) { // check form fields //
      this.uploadFile(error_file); // upload file to server //
    };
    return false;
  };

  // submit name, file to server for processing //
  uploadFile = formData=>  {
    this.restService.uploadAlsFile(this.file).subscribe(
      data => console.log(data), 
      error => console.log("Dispay ERROR"), 
      () => console.log("GO TO NEXT"));
  };

  ngOnInit() {
  }

}
