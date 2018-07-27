import { Injectable } from '@angular/core';
import { HttpHeaders, HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class RestService {
  formList:Object;
  constructor(private http:HttpClient) {
    this.formList = {'formsList':[]};
  }

  // load data from local file for testing //  
  loadFormListData = () => this.http.get('assets/formListData.json');
  
  // upload file service //
  uploadAlsFile = file => this.http.post('http://localhost:8080/gateway/parseservice?owner=me2',file,{
    observe:"events",
    reportProgress:true
  }
    );
  
}
