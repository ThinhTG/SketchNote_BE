// ChatScreen.tsx - React Native Example
// Install: npm install @stomp/stompjs sockjs-client

import React, { useState, useEffect, useRef } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  FlatList,
  StyleSheet,
  KeyboardAvoidingView,
  Platform,
} from 'react-native';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

interface ChatMessage {
  type: 'JOIN' | 'LEAVE' | 'CHAT' | 'TYPING';
  senderId: number;
  senderName: string;
  receiverId?: number;
  content: string;
  timestamp: string;
}

interface ChatScreenProps {
  currentUser: {
    id: number;
    name: string;
    avatarUrl?: string;
  };
  receiverUser?: {
    id: number;
    name: string;
  };
  chatType?: 'public' | 'private' | 'project';
  projectId?: number;
}

export default function ChatScreen({
  currentUser,
  receiverUser,
  chatType = 'private',
  projectId,
}: ChatScreenProps) {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [inputText, setInputText] = useState('');
  const [isConnected, setIsConnected] = useState(false);
  const [isTyping, setIsTyping] = useState(false);
  const stompClient = useRef<Client | null>(null);
  const typingTimeout = useRef<NodeJS.Timeout | null>(null);

  useEffect(() => {
    connectWebSocket();
    return () => {
      disconnectWebSocket();
    };
  }, []);

  const connectWebSocket = () => {
    const socket = new SockJS('http://YOUR_SERVER:8082/ws');
    const client = new Client({
      webSocketFactory: () => socket as any,
      onConnect: () => {
        console.log('Connected to WebSocket');
        setIsConnected(true);

        // Subscribe based on chat type
        if (chatType === 'public') {
          client.subscribe('/topic/public', handleMessage);
        } else if (chatType === 'private' && receiverUser) {
          client.subscribe(`/queue/private/${currentUser.id}`, handleMessage);
          client.subscribe(`/queue/typing/${currentUser.id}`, handleTyping);
        } else if (chatType === 'project' && projectId) {
          client.subscribe(`/topic/project/${projectId}`, handleMessage);
        }

        // Join the chat
        client.publish({
          destination: '/app/chat.addUser',
          body: JSON.stringify({
            senderId: currentUser.id,
            senderName: currentUser.name,
          }),
        });
      },
      onDisconnect: () => {
        console.log('Disconnected from WebSocket');
        setIsConnected(false);
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame);
      },
    });

    client.activate();
    stompClient.current = client;
  };

  const disconnectWebSocket = () => {
    if (stompClient.current) {
      stompClient.current.deactivate();
    }
  };

  const handleMessage = (message: any) => {
    const chatMessage: ChatMessage = JSON.parse(message.body);
    setMessages((prev) => [...prev, chatMessage]);
  };

  const handleTyping = (message: any) => {
    const typing = JSON.parse(message.body);
    setIsTyping(typing.isTyping);
    if (typing.isTyping) {
      setTimeout(() => setIsTyping(false), 3000);
    }
  };

  const sendMessage = () => {
    if (!inputText.trim() || !stompClient.current || !isConnected) return;

    const message: any = {
      senderId: currentUser.id,
      senderName: currentUser.name,
      content: inputText,
    };

    let destination = '/app/chat.sendMessage';

    if (chatType === 'private' && receiverUser) {
      message.receiverId = receiverUser.id;
      destination = '/app/chat.private';
    } else if (chatType === 'project' && projectId) {
      message.projectId = projectId;
      destination = '/app/chat.project';
    }

    stompClient.current.publish({
      destination,
      body: JSON.stringify(message),
    });

    setInputText('');
  };

  const handleTextChange = (text: string) => {
    setInputText(text);

    // Send typing indicator for private chat
    if (chatType === 'private' && receiverUser && stompClient.current) {
      // Clear previous timeout
      if (typingTimeout.current) {
        clearTimeout(typingTimeout.current);
      }

      // Send typing started
      stompClient.current.publish({
        destination: '/app/chat.typing',
        body: JSON.stringify({
          userId: currentUser.id,
          userName: currentUser.name,
          receiverId: receiverUser.id,
          isTyping: true,
        }),
      });

      // Set timeout to send typing stopped
      typingTimeout.current = setTimeout(() => {
        if (stompClient.current) {
          stompClient.current.publish({
            destination: '/app/chat.typing',
            body: JSON.stringify({
              userId: currentUser.id,
              userName: currentUser.name,
              receiverId: receiverUser.id,
              isTyping: false,
            }),
          });
        }
      }, 1000);
    }
  };

  const renderMessage = ({ item }: { item: ChatMessage }) => {
    const isSent = item.senderId === currentUser.id;

    if (item.type === 'JOIN' || item.type === 'LEAVE') {
      return (
        <View style={styles.systemMessage}>
          <Text style={styles.systemMessageText}>
            {item.senderName} {item.type === 'JOIN' ? 'joined' : 'left'} the chat
          </Text>
        </View>
      );
    }

    return (
      <View style={[styles.messageContainer, isSent ? styles.sentMessage : styles.receivedMessage]}>
        {!isSent && <Text style={styles.senderName}>{item.senderName}</Text>}
        <Text style={[styles.messageText, isSent && styles.sentMessageText]}>
          {item.content}
        </Text>
        <Text style={[styles.timestamp, isSent && styles.sentTimestamp]}>
          {new Date(item.timestamp).toLocaleTimeString()}
        </Text>
      </View>
    );
  };

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
      keyboardVerticalOffset={90}
    >
      {/* Header */}
      <View style={styles.header}>
        <Text style={styles.headerTitle}>
          {chatType === 'public'
            ? 'Public Chat'
            : chatType === 'project'
            ? `Project #${projectId}`
            : receiverUser?.name || 'Chat'}
        </Text>
        <View style={[styles.statusDot, isConnected && styles.statusDotConnected]} />
      </View>

      {/* Typing Indicator */}
      {isTyping && (
        <View style={styles.typingIndicator}>
          <Text style={styles.typingText}>
            {receiverUser?.name || 'Someone'} is typing...
          </Text>
        </View>
      )}

      {/* Messages */}
      <FlatList
        data={messages}
        renderItem={renderMessage}
        keyExtractor={(item, index) => index.toString()}
        contentContainerStyle={styles.messagesList}
        inverted={false}
      />

      {/* Input */}
      <View style={styles.inputContainer}>
        <TextInput
          style={styles.input}
          value={inputText}
          onChangeText={handleTextChange}
          placeholder="Type a message..."
          multiline
          editable={isConnected}
        />
        <TouchableOpacity
          style={[styles.sendButton, !isConnected && styles.sendButtonDisabled]}
          onPress={sendMessage}
          disabled={!isConnected || !inputText.trim()}
        >
          <Text style={styles.sendButtonText}>Send</Text>
        </TouchableOpacity>
      </View>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    padding: 16,
    backgroundColor: '#6366f1',
    borderBottomWidth: 1,
    borderBottomColor: '#e0e0e0',
  },
  headerTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#fff',
  },
  statusDot: {
    width: 12,
    height: 12,
    borderRadius: 6,
    backgroundColor: '#ef4444',
  },
  statusDotConnected: {
    backgroundColor: '#22c55e',
  },
  typingIndicator: {
    padding: 8,
    paddingHorizontal: 16,
    backgroundColor: '#e0e7ff',
  },
  typingText: {
    fontSize: 12,
    fontStyle: 'italic',
    color: '#6366f1',
  },
  messagesList: {
    padding: 16,
  },
  messageContainer: {
    maxWidth: '75%',
    marginBottom: 12,
    padding: 12,
    borderRadius: 16,
  },
  sentMessage: {
    alignSelf: 'flex-end',
    backgroundColor: '#6366f1',
  },
  receivedMessage: {
    alignSelf: 'flex-start',
    backgroundColor: '#fff',
    borderWidth: 1,
    borderColor: '#e0e0e0',
  },
  senderName: {
    fontSize: 12,
    fontWeight: '600',
    marginBottom: 4,
    color: '#666',
  },
  messageText: {
    fontSize: 16,
    color: '#333',
  },
  sentMessageText: {
    color: '#fff',
  },
  timestamp: {
    fontSize: 10,
    marginTop: 4,
    color: '#999',
  },
  sentTimestamp: {
    color: '#e0e7ff',
  },
  systemMessage: {
    alignSelf: 'center',
    backgroundColor: '#e0f2fe',
    paddingVertical: 6,
    paddingHorizontal: 12,
    borderRadius: 12,
    marginVertical: 8,
  },
  systemMessageText: {
    fontSize: 12,
    color: '#0284c7',
  },
  inputContainer: {
    flexDirection: 'row',
    padding: 16,
    backgroundColor: '#fff',
    borderTopWidth: 1,
    borderTopColor: '#e0e0e0',
  },
  input: {
    flex: 1,
    backgroundColor: '#f5f5f5',
    borderRadius: 20,
    paddingHorizontal: 16,
    paddingVertical: 8,
    marginRight: 8,
    maxHeight: 100,
    fontSize: 16,
  },
  sendButton: {
    backgroundColor: '#6366f1',
    borderRadius: 20,
    paddingHorizontal: 20,
    paddingVertical: 10,
    justifyContent: 'center',
  },
  sendButtonDisabled: {
    backgroundColor: '#cbd5e1',
  },
  sendButtonText: {
    color: '#fff',
    fontWeight: '600',
    fontSize: 16,
  },
});

// Usage Example:
/*
import ChatScreen from './ChatScreen';

function App() {
  const currentUser = {
    id: 1,
    name: 'John Doe',
    avatarUrl: 'https://example.com/avatar.jpg'
  };

  const receiverUser = {
    id: 2,
    name: 'Jane Smith'
  };

  return (
    <ChatScreen
      currentUser={currentUser}
      receiverUser={receiverUser}
      chatType="private"
    />
  );
}
*/
