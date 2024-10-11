import { Injectable } from '@angular/core';
import { Client } from '@stomp/stompjs';
import { BehaviorSubject } from 'rxjs';
import * as SockJS from 'sockjs-client';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private client: Client;
  private readonly webSocketUrl = 'http://localhost:8080/ws';
  public messages: BehaviorSubject<string> = new BehaviorSubject('');

  constructor() {
    this.client = new Client({
      webSocketFactory: () => new SockJS(this.webSocketUrl),
      onConnect: () => {
        console.log('Connected to WebSocket');
        this.client.subscribe('/topic', message => {
          this.messages.next(message.body);
        });
      },
      onStompError: (frame) => {
        console.error('Broker reported error: ' + frame.headers['message']);
        console.error('Additional details: ' + frame.body);
      }
    });

    this.client.activate();
  }
}
