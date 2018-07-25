import { Injectable } from '@angular/core';
import { HttpHeaders, HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class RestService {

  constructor(private http:HttpClient) {
   }

  loadFormListData() {
      return this.http.get('assets/formListData.json')
  };
}
