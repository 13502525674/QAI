const BASE_URL = "http://localhost:8080";

export function streamChat(message, conversationId, onMessage, onError, onComplete) {
  const token = window.localStorage.getItem("user_token");
  const url = `${BASE_URL}/ai/chat/stream?message=${encodeURIComponent(message)}&conversationId=${conversationId || 'default'}`;

  fetch(url, {
    method: 'GET',
    headers: {
      'x_access_token': token || ''
    }
  }).then(response => {
    if (!response.ok) {
      throw new Error('Network response was not ok');
    }
    const reader = response.body.getReader();
    const decoder = new TextDecoder("utf-8");
    let buffer = '';

    function read() {
      reader.read().then(({ done, value }) => {
        if (done) {
          // Process any remaining buffer content
          if (buffer.trim() !== '') {
             const lines = buffer.split('\n');
             for (const line of lines) {
                 if (line.trim() === '') continue;
                 if (line.startsWith('data:')) {
                     if (onMessage) onMessage(line.substring(5));
                 } else {
                     if (onMessage) onMessage(line);
                 }
             }
          }
          if (onComplete) onComplete();
          return;
        }
        const chunk = decoder.decode(value, { stream: true });
        
        // If content is plain text (not SSE format), just pass it
        // This is a fallback if backend doesn't send "data:" prefix
        if (!chunk.includes('data:') && !buffer.includes('data:')) {
             if (onMessage) onMessage(chunk);
             read();
             return;
        }

        buffer += chunk;
        
        const lines = buffer.split('\n');
        buffer = lines.pop(); // Keep the last incomplete line in buffer

        for (const line of lines) {
            if (line.trim() === '') continue;
            if (line.startsWith('data:')) {
                const data = line.substring(5); 
                if (onMessage) onMessage(data);
            } else {
                 // Try to handle lines that might be raw text mixed with SSE or malformed
                 if (onMessage) onMessage(line);
            }
        }
        
        read();
      }).catch(error => {
        if (onError) onError(error);
      });
    }
    read();
  }).catch(error => {
    if (onError) onError(error);
  });
}

export function getHistory(conversationId) {
  const token = window.localStorage.getItem("user_token");
  const url = `${BASE_URL}/ai/history?conversationId=${conversationId || 'default'}`;
  return fetch(url, {
      method: 'GET',
      headers: {
        'x_access_token': token || ''
      }
  }).then(response => {
      if (!response.ok) {
          throw new Error('Network response was not ok');
      }
      return response.json();
  });
}

export function clearHistory(conversationId) {
    const token = window.localStorage.getItem("user_token");
    const url = `${BASE_URL}/ai/history?conversationId=${conversationId || 'default'}`;
    return fetch(url, {
        method: 'DELETE',
        headers: {
            'x_access_token': token || ''
        }
    }).then(response => {
        if (!response.ok) {
            throw new Error('Failed to clear history');
        }
        return response.text();
    });
}

export function recognizeSpeech(audioBlob) {
    const token = window.localStorage.getItem("user_token");
    const url = `${BASE_URL}/speech/recognize`;
    
    const formData = new FormData();
    formData.append('audio', audioBlob, 'audio.wav');
    
    return fetch(url, {
        method: 'POST',
        headers: {
            'x_access_token': token || ''
        },
        body: formData
    }).then(response => {
        if (!response.ok) {
            throw new Error('Speech recognition failed');
        }
        return response.json();
    });
}

export function synthesizeSpeech(text) {
    const token = window.localStorage.getItem("user_token");
    const url = `${BASE_URL}/tts/synthesize`;
    
    return fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'x_access_token': token || ''
        },
        body: JSON.stringify({ text })
    }).then(response => {
        if (!response.ok) {
            throw new Error('Speech synthesis failed');
        }
        return response.json();
    });
}

export function synthesizeSpeechStream(text, onAudioChunk, onError, onComplete) {
    const token = window.localStorage.getItem("user_token");
    const params = new URLSearchParams({
        text: text,
        x_access_token: token || ''
    });
    const url = `${BASE_URL}/tts/stream?${params.toString()}`;
    
    const eventSource = new EventSource(url);
    
    eventSource.addEventListener('audio', (event) => {
        if (onAudioChunk) {
            onAudioChunk(event.data);
        }
    });
    
    eventSource.addEventListener('done', () => {
        eventSource.close();
        if (onComplete) onComplete();
    });
    
    eventSource.addEventListener('error', (event) => {
        eventSource.close();
        if (onError) {
            onError(new Error(event.data || 'Stream error'));
        }
    });
    
    eventSource.onerror = (error) => {
        eventSource.close();
        if (onError) onError(error);
    };
    
    return eventSource;
}
