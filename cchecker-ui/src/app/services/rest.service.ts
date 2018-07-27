import { Injectable } from '@angular/core';
import { HttpHeaders, HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})

// possibly move setters and getters out into data service //
export class RestService {
  formList:Object;
  constructor(private http:HttpClient) {
    this.formList = {'formsList':[]};
  }

  // upload file service //
  uploadAlsFile = file => this.http.post('http://localhost:8080/gateway/parseservice?owner=me2',file,{
    observe:"events",
    reportProgress:true
    }
  );

  // store local copy of formList //
  storeLocalFormListData = (data) => {
    this.formList = data; // can probably remove this line
    sessionStorage.setItem('formList',JSON.stringify(data)); // convert to string //
  }

  // get formList data //
  getFormListData = () => sessionStorage.getItem('formList') ? JSON.parse(sessionStorage.getItem('formList')): this.formList;
  
}
