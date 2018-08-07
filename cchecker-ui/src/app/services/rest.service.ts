import { Injectable } from '@angular/core';
import { HttpHeaders, HttpClient, HttpProgressEvent, HttpEvent, HttpEventType } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})

export class RestService {
  constructor(private http:HttpClient) {
  }

  // upload file service //
  uploadAlsFile(file){
    return this.http.post('http://localhost:8080/gateway/parseservice?owner=me2',file,
    {
      observe:"events",
      reportProgress:true
    })} ;
}
