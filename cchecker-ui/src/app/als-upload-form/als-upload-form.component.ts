import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-als-upload-form',
  templateUrl: './als-upload-form.component.html',
  styleUrls: ['./als-upload-form.component.css']
})
export class AlsUploadFormComponent implements OnInit {
  name:string;
  alsFile:File;
  submitted:boolean;
  
  constructor(private router:Router) { 
    this.submitted = false;
  }

  // user clicked submit //
  // validate form fields are valid and upload //
  submitForm(error_name, error_file) {
    this.submitted = true; // set for form validation //
    if (error_name.valid && error_file.valid) { // check form fields //
      this.uploadFile(); // upload file to server //
    };
    return false;
  };

  // submit name, file to server for processing //
  uploadFile() {
    console.log("Submit to server");
    this.router.navigate(['/forms'])

    return false;
  };

  ngOnInit() {
  }

}
