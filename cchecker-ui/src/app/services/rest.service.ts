import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';

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
      reportProgress:true,
      withCredentials:true,
    })} ;

  // upload file service //
  checkForms(checkedItems,formListData){
    const checkUom = formListData['checkUom'] ? 'true':'false';
    const checkCRF = formListData['checkStdCrfCde'] ? 'true':'false';
    const displayExceptions = formListData['mustDisplayException'] ? 'true':'false';
    return this.http.post('http://localhost:8080/gateway/checkservice',checkedItems,
    {
      withCredentials:true,
      params:new HttpParams().set('checkUom', checkUom).append('checkCRF',checkCRF).append('displayExceptions',displayExceptions)
    })
  } ;    
}
