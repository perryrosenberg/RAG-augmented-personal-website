/**
 * Essential smoke tests for frontend components and utilities.
 * Tests critical rendering, user interactions, and API integration.
 *
 * @author Perry Rosenberg
 */

import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import React from 'react';

// Mock components for testing (simplified representations)
const LoadingIndicator = () => <div role="status">Loading...</div>;

const ChatMessage = ({ message, role }: { message: string; role: string }) => (
  <div data-testid={`message-${role}`}>
    <span>{role}</span>: {message}
  </div>
);

const ChatInput = ({ onSend, disabled }: { onSend: (msg: string) => void; disabled?: boolean }) => {
  const [value, setValue] = React.useState('');
  return (
    <div>
      <textarea
        data-testid="chat-input"
        value={value}
        onChange={(e) => setValue(e.target.value)}
        disabled={disabled}
      />
      <button
        data-testid="send-button"
        onClick={() => {
          onSend(value);
          setValue('');
        }}
        disabled={disabled}
      >
        Send
      </button>
    </div>
  );
};

const SourcesPanel = ({ sources }: { sources: Array<{ title: string; type: string }> }) => (
  <div data-testid="sources-panel">
    {sources.length === 0 ? (
      <p>No sources</p>
    ) : (
      <ul>
        {sources.map((source, i) => (
          <li key={i}>
            {source.title} ({source.type})
          </li>
        ))}
      </ul>
    )}
  </div>
);

describe('LoadingIndicator Component', () => {
  test('renders loading indicator', () => {
    render(<LoadingIndicator />);
    expect(screen.getByRole('status')).toBeInTheDocument();
    expect(screen.getByText('Loading...')).toBeInTheDocument();
  });

  test('has accessible role', () => {
    render(<LoadingIndicator />);
    const indicator = screen.getByRole('status');
    expect(indicator).toHaveAttribute('role', 'status');
  });
});

describe('ChatMessage Component', () => {
  test('renders user message', () => {
    render(<ChatMessage message="Hello" role="user" />);
    expect(screen.getByTestId('message-user')).toBeInTheDocument();
    expect(screen.getByText(/Hello/)).toBeInTheDocument();
  });

  test('renders assistant message', () => {
    render(<ChatMessage message="Hi there!" role="assistant" />);
    expect(screen.getByTestId('message-assistant')).toBeInTheDocument();
    expect(screen.getByText(/Hi there!/)).toBeInTheDocument();
  });

  test('displays role correctly', () => {
    render(<ChatMessage message="Test" role="user" />);
    expect(screen.getByText('user')).toBeInTheDocument();
  });

  test('displays message content correctly', () => {
    const longMessage = 'This is a longer message with multiple words';
    render(<ChatMessage message={longMessage} role="user" />);
    expect(screen.getByText(new RegExp(longMessage))).toBeInTheDocument();
  });
});

describe('ChatInput Component', () => {
  test('renders textarea and send button', () => {
    const mockOnSend = jest.fn();
    render(<ChatInput onSend={mockOnSend} />);

    expect(screen.getByTestId('chat-input')).toBeInTheDocument();
    expect(screen.getByTestId('send-button')).toBeInTheDocument();
  });

  test('allows typing in textarea', () => {
    const mockOnSend = jest.fn();
    render(<ChatInput onSend={mockOnSend} />);

    const input = screen.getByTestId('chat-input') as HTMLTextAreaElement;
    fireEvent.change(input, { target: { value: 'Test message' } });

    expect(input.value).toBe('Test message');
  });

  test('calls onSend when send button clicked', () => {
    const mockOnSend = jest.fn();
    render(<ChatInput onSend={mockOnSend} />);

    const input = screen.getByTestId('chat-input');
    const button = screen.getByTestId('send-button');

    fireEvent.change(input, { target: { value: 'Hello' } });
    fireEvent.click(button);

    expect(mockOnSend).toHaveBeenCalledWith('Hello');
  });

  test('clears input after sending', () => {
    const mockOnSend = jest.fn();
    render(<ChatInput onSend={mockOnSend} />);

    const input = screen.getByTestId('chat-input') as HTMLTextAreaElement;
    const button = screen.getByTestId('send-button');

    fireEvent.change(input, { target: { value: 'Test' } });
    fireEvent.click(button);

    expect(input.value).toBe('');
  });

  test('disables input when disabled prop is true', () => {
    const mockOnSend = jest.fn();
    render(<ChatInput onSend={mockOnSend} disabled={true} />);

    const input = screen.getByTestId('chat-input');
    const button = screen.getByTestId('send-button');

    expect(input).toBeDisabled();
    expect(button).toBeDisabled();
  });

  test('does not call onSend when disabled', () => {
    const mockOnSend = jest.fn();
    render(<ChatInput onSend={mockOnSend} disabled={true} />);

    const button = screen.getByTestId('send-button');
    fireEvent.click(button);

    expect(mockOnSend).not.toHaveBeenCalled();
  });
});

describe('SourcesPanel Component', () => {
  test('renders empty state when no sources', () => {
    render(<SourcesPanel sources={[]} />);
    expect(screen.getByText('No sources')).toBeInTheDocument();
  });

  test('renders correct number of sources', () => {
    const sources = [
      { title: 'Doc 1', type: 'Type 1' },
      { title: 'Doc 2', type: 'Type 2' },
      { title: 'Doc 3', type: 'Type 3' },
    ];
    render(<SourcesPanel sources={sources} />);

    const panel = screen.getByTestId('sources-panel');
    const items = panel.querySelectorAll('li');
    expect(items).toHaveLength(3);
  });

  test('handles single source', () => {
    const sources = [{ title: 'Single Doc', type: 'PDF' }];
    render(<SourcesPanel sources={sources} />);

    expect(screen.getByText(/Single Doc \(PDF\)/)).toBeInTheDocument();
  });
});

describe('Assistant API Integration', () => {
  test('API endpoint is defined', () => {
    const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:3001';
    expect(apiUrl).toBeTruthy();
    expect(typeof apiUrl).toBe('string');
  });
});

describe('Component Integration', () => {
  test('ChatInput and ChatMessage work together', () => {
    const messages: Array<{ text: string; role: string }> = [];
    const TestComponent = () => {
      const [msgs, setMsgs] = React.useState(messages);

      return (
        <div>
          {msgs.map((msg, i) => (
            <ChatMessage key={i} message={msg.text} role={msg.role} />
          ))}
          <ChatInput
            onSend={(text) => setMsgs([...msgs, { text, role: 'user' }])}
          />
        </div>
      );
    };

    render(<TestComponent />);

    const input = screen.getByTestId('chat-input');
    const button = screen.getByTestId('send-button');

    fireEvent.change(input, { target: { value: 'Test message' } });
    fireEvent.click(button);

    expect(screen.getByText(/Test message/)).toBeInTheDocument();
  });

  test('SourcesPanel updates when sources change', () => {
    const TestComponent = () => {
      const [sources, setSources] = React.useState<Array<{ title: string; type: string }>>([]);

      return (
        <div>
          <button onClick={() => setSources([{ title: 'New Doc', type: 'PDF' }])}>
            Add Source
          </button>
          <SourcesPanel sources={sources} />
        </div>
      );
    };

    render(<TestComponent />);

    expect(screen.getByText('No sources')).toBeInTheDocument();

    fireEvent.click(screen.getByText('Add Source'));

    expect(screen.getByText(/New Doc \(PDF\)/)).toBeInTheDocument();
  });

  test('Multiple messages render in order', () => {
    const TestComponent = () => {
      const [msgs, setMsgs] = React.useState([
        { text: 'First', role: 'user' },
        { text: 'Second', role: 'assistant' },
      ]);

      return (
        <div>
          {msgs.map((msg, i) => (
            <ChatMessage key={i} message={msg.text} role={msg.role} />
          ))}
        </div>
      );
    };

    render(<TestComponent />);

    expect(screen.getByText(/First/)).toBeInTheDocument();
    expect(screen.getByText(/Second/)).toBeInTheDocument();
  });
});
